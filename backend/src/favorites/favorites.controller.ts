import {
  Controller,
  Delete,
  Get,
  HttpCode,
  Param,
  ParseIntPipe,
  Put,
  UseGuards,
} from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../auth/current-user.decorator';
import { AuthUser, JwtAuthGuard } from '../auth/jwt-auth.guard';
import { PrismaService } from '../prisma/prisma.service';

@ApiTags('favorites')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('favorites')
export class FavoritesController {
  constructor(private readonly prisma: PrismaService) {}

  @Get()
  async list(@CurrentUser() user: AuthUser) {
    const rows = await this.prisma.favorite.findMany({
      where: { userId: user.id },
      select: { specialistId: true },
    });
    return rows.map((r) => r.specialistId);
  }

  @Put(':specialistId')
  @HttpCode(204)
  async add(
    @CurrentUser() user: AuthUser,
    @Param('specialistId', ParseIntPipe) specialistId: number,
  ) {
    await this.prisma.favorite.upsert({
      where: { userId_specialistId: { userId: user.id, specialistId } },
      update: {},
      create: { userId: user.id, specialistId },
    });
  }

  @Delete(':specialistId')
  @HttpCode(204)
  async remove(
    @CurrentUser() user: AuthUser,
    @Param('specialistId', ParseIntPipe) specialistId: number,
  ) {
    await this.prisma.favorite.deleteMany({
      where: { userId: user.id, specialistId },
    });
  }
}
