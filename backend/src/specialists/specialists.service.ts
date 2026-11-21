import { Injectable, NotFoundException } from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { buildSlots, weekdayOf } from '../common/time';
import { PrismaService } from '../prisma/prisma.service';
import { SpecialistQueryDto } from './specialists.dto';

@Injectable()
export class SpecialistsService {
  constructor(private readonly prisma: PrismaService) {}

  categories() {
    return this.prisma.category.findMany({ orderBy: { id: 'asc' } });
  }

  cities() {
    return this.prisma.specialist
      .findMany({ distinct: ['city'], select: { city: true } })
      .then((rows) => rows.map((r) => r.city).sort());
  }

  async list(query: SpecialistQueryDto, userId: number) {
    const where: Prisma.SpecialistWhereInput = {};
    if (query.city) where.city = query.city;
    if (query.category) where.category = { slug: query.category };
    if (query.q) {
      const contains = { contains: query.q, mode: 'insensitive' as const };
      where.OR = [
        { name: contains },
        { title: contains },
        { services: { some: { name: contains } } },
      ];
    }

    const specialists = await this.prisma.specialist.findMany({
      where,
      include: {
        category: true,
        services: { select: { price: true } },
        favorites: { where: { userId }, select: { userId: true } },
      },
    });

    const items = specialists.map(({ services, favorites, ...s }) => ({
      ...s,
      priceFrom: Math.min(...services.map((x) => x.price)),
      isFavorite: favorites.length > 0,
    }));

    if (query.sort === 'price') {
      items.sort((a, b) => a.priceFrom - b.priceFrom);
    } else {
      items.sort((a, b) => b.rating - a.rating);
    }
    return items;
  }

  async details(id: number, userId: number) {
    const specialist = await this.prisma.specialist.findUnique({
      where: { id },
      include: {
        category: true,
        services: { orderBy: { id: 'asc' } },
        workingHours: { orderBy: { weekday: 'asc' } },
        favorites: { where: { userId }, select: { userId: true } },
      },
    });
    if (!specialist) throw new NotFoundException('Specialist not found');
    const { favorites, ...rest } = specialist;
    return { ...rest, isFavorite: favorites.length > 0 };
  }

  async reviews(id: number) {
    const reviews = await this.prisma.review.findMany({
      where: { specialistId: id },
      orderBy: { createdAt: 'desc' },
      include: {
        client: { select: { name: true } },
        booking: { select: { service: { select: { name: true } } } },
      },
    });
    return reviews.map((r) => ({
      id: r.id,
      rating: r.rating,
      comment: r.comment,
      createdAt: r.createdAt,
      clientName: r.client.name ?? 'Client',
      serviceName: r.booking.service.name,
    }));
  }

  async slots(specialistId: number, serviceId: number, date: string) {
    const service = await this.prisma.service.findFirst({
      where: { id: serviceId, specialistId },
    });
    if (!service) throw new NotFoundException('Service not found');

    const hours = await this.prisma.workingHours.findUnique({
      where: {
        specialistId_weekday: { specialistId, weekday: weekdayOf(date) },
      },
    });
    if (!hours) return [];

    const dayStart = new Date(`${date}T00:00:00+03:00`);
    const dayEnd = new Date(dayStart.getTime() + 86_400_000);
    const busy = await this.prisma.booking.findMany({
      where: {
        specialistId,
        status: { in: ['PENDING', 'CONFIRMED'] },
        startsAt: { lt: dayEnd },
        endsAt: { gt: dayStart },
      },
      select: { startsAt: true, endsAt: true },
    });

    return buildSlots(
      date,
      hours.startMinute,
      hours.endMinute,
      service.durationMin,
      busy,
      new Date(),
    );
  }
}
