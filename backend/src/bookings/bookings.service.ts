import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { ISTANBUL_OFFSET_MINUTES, weekdayOf } from '../common/time';
import { PrismaService } from '../prisma/prisma.service';
import { CreateBookingDto, CreateReviewDto } from './bookings.dto';

const ACTIVE_STATUSES = ['PENDING', 'CONFIRMED'] as const;

const bookingInclude = {
  service: { select: { name: true, durationMin: true } },
  specialist: {
    select: { id: true, name: true, title: true, avatarColor: true },
  },
  review: { select: { rating: true } },
} satisfies Prisma.BookingInclude;

/** Postgres raises 23P01 when the exclusion constraint "Booking_no_overlap" fails. */
function isOverlapError(error: unknown): boolean {
  return error instanceof Error && error.message.includes('Booking_no_overlap');
}

@Injectable()
export class BookingsService {
  constructor(private readonly prisma: PrismaService) {}

  async create(clientId: number, dto: CreateBookingDto) {
    const service = await this.prisma.service.findUnique({
      where: { id: dto.serviceId },
    });
    if (!service) throw new NotFoundException('Service not found');

    const startsAt = new Date(dto.startsAt);
    const endsAt = new Date(startsAt.getTime() + service.durationMin * 60_000);
    if (startsAt <= new Date()) {
      throw new BadRequestException('Time is in the past');
    }

    // Check that the visit is inside the working hours of that day (Istanbul time).
    const local = new Date(
      startsAt.getTime() + ISTANBUL_OFFSET_MINUTES * 60_000,
    );
    const date = local.toISOString().slice(0, 10);
    const minute = local.getUTCHours() * 60 + local.getUTCMinutes();
    const hours = await this.prisma.workingHours.findUnique({
      where: {
        specialistId_weekday: {
          specialistId: service.specialistId,
          weekday: weekdayOf(date),
        },
      },
    });
    if (
      !hours ||
      minute < hours.startMinute ||
      minute + service.durationMin > hours.endMinute
    ) {
      throw new BadRequestException('Outside of working hours');
    }

    try {
      return await this.prisma.booking.create({
        data: {
          clientId,
          specialistId: service.specialistId,
          serviceId: service.id,
          startsAt,
          endsAt,
          price: service.price,
        },
        include: bookingInclude,
      });
    } catch (error) {
      if (isOverlapError(error)) {
        throw new ConflictException('This time is already taken');
      }
      throw error;
    }
  }

  my(clientId: number, scope: 'upcoming' | 'past') {
    const now = new Date();
    const where: Prisma.BookingWhereInput =
      scope === 'upcoming'
        ? {
            clientId,
            status: { in: [...ACTIVE_STATUSES] },
            endsAt: { gt: now },
          }
        : {
            clientId,
            OR: [
              { status: { in: ['COMPLETED', 'CANCELLED', 'REJECTED'] } },
              { endsAt: { lte: now } },
            ],
          };
    return this.prisma.booking.findMany({
      where,
      include: bookingInclude,
      orderBy: { startsAt: scope === 'upcoming' ? 'asc' : 'desc' },
    });
  }

  async cancel(clientId: number, id: number) {
    const booking = await this.findOwn(clientId, id);
    if (
      !ACTIVE_STATUSES.some((s) => s === booking.status) ||
      booking.startsAt <= new Date()
    ) {
      throw new BadRequestException('This booking can not be cancelled');
    }
    return this.prisma.booking.update({
      where: { id },
      data: { status: 'CANCELLED' },
      include: bookingInclude,
    });
  }

  async review(clientId: number, id: number, dto: CreateReviewDto) {
    const booking = await this.findOwn(clientId, id);
    if (booking.status !== 'COMPLETED') {
      throw new BadRequestException('Only completed visits can be reviewed');
    }
    const existing = await this.prisma.review.findUnique({
      where: { bookingId: id },
    });
    if (existing) throw new ConflictException('Already reviewed');

    return this.prisma.$transaction(async (tx) => {
      const review = await tx.review.create({
        data: {
          bookingId: id,
          clientId,
          specialistId: booking.specialistId,
          rating: dto.rating,
          comment: dto.comment,
        },
      });
      // Running average, so the rating also counts reviews made before this app.
      const specialist = await tx.specialist.findUniqueOrThrow({
        where: { id: booking.specialistId },
      });
      const count = specialist.reviewCount + 1;
      const rating =
        (specialist.rating * specialist.reviewCount + dto.rating) / count;
      await tx.specialist.update({
        where: { id: booking.specialistId },
        data: { rating: Math.round(rating * 10) / 10, reviewCount: count },
      });
      return review;
    });
  }

  private async findOwn(clientId: number, id: number) {
    const booking = await this.prisma.booking.findUnique({ where: { id } });
    if (!booking) throw new NotFoundException('Booking not found');
    if (booking.clientId !== clientId) throw new ForbiddenException();
    return booking;
  }
}
