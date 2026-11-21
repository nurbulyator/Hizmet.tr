import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { AuthModule } from './auth/auth.module';
import { BookingsModule } from './bookings/bookings.module';
import { FavoritesController } from './favorites/favorites.controller';
import { PrismaModule } from './prisma/prisma.module';
import { SpecialistsModule } from './specialists/specialists.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    PrismaModule,
    AuthModule,
    SpecialistsModule,
    BookingsModule,
  ],
  controllers: [FavoritesController],
})
export class AppModule {}
