import {
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../auth/current-user.decorator';
import { AuthUser, JwtAuthGuard } from '../auth/jwt-auth.guard';
import { SlotsQueryDto, SpecialistQueryDto } from './specialists.dto';
import { SpecialistsService } from './specialists.service';

@ApiTags('catalog')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller()
export class SpecialistsController {
  constructor(private readonly specialists: SpecialistsService) {}

  @Get('categories')
  categories() {
    return this.specialists.categories();
  }

  @Get('cities')
  cities() {
    return this.specialists.cities();
  }

  @Get('specialists')
  list(@Query() query: SpecialistQueryDto, @CurrentUser() user: AuthUser) {
    return this.specialists.list(query, user.id);
  }

  @Get('specialists/:id')
  details(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: AuthUser,
  ) {
    return this.specialists.details(id, user.id);
  }

  @Get('specialists/:id/reviews')
  reviews(@Param('id', ParseIntPipe) id: number) {
    return this.specialists.reviews(id);
  }

  @Get('specialists/:id/slots')
  slots(@Param('id', ParseIntPipe) id: number, @Query() query: SlotsQueryDto) {
    return this.specialists.slots(id, query.serviceId, query.date);
  }
}
