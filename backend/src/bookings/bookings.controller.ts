import {
  Body,
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../auth/current-user.decorator';
import { AuthUser, JwtAuthGuard } from '../auth/jwt-auth.guard';
import {
  CreateBookingDto,
  CreateReviewDto,
  MyBookingsQueryDto,
} from './bookings.dto';
import { BookingsService } from './bookings.service';

@ApiTags('bookings')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('bookings')
export class BookingsController {
  constructor(private readonly bookings: BookingsService) {}

  @Post()
  create(@CurrentUser() user: AuthUser, @Body() dto: CreateBookingDto) {
    return this.bookings.create(user.id, dto);
  }

  @Get('my')
  my(@CurrentUser() user: AuthUser, @Query() query: MyBookingsQueryDto) {
    return this.bookings.my(user.id, query.scope ?? 'upcoming');
  }

  @Patch(':id/cancel')
  cancel(@CurrentUser() user: AuthUser, @Param('id', ParseIntPipe) id: number) {
    return this.bookings.cancel(user.id, id);
  }

  @Post(':id/review')
  review(
    @CurrentUser() user: AuthUser,
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: CreateReviewDto,
  ) {
    return this.bookings.review(user.id, id, dto);
  }
}
