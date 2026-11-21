import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsDateString,
  IsIn,
  IsInt,
  IsOptional,
  IsString,
  Max,
  MaxLength,
  Min,
} from 'class-validator';

export class CreateBookingDto {
  @ApiProperty()
  @IsInt()
  serviceId!: number;

  @ApiProperty({ example: '2026-10-21T11:00:00.000Z' })
  @IsDateString()
  startsAt!: string;
}

export class MyBookingsQueryDto {
  @ApiPropertyOptional({ enum: ['upcoming', 'past'] })
  @IsOptional()
  @IsIn(['upcoming', 'past'])
  scope?: 'upcoming' | 'past';
}

export class CreateReviewDto {
  @ApiProperty({ minimum: 1, maximum: 5 })
  @IsInt()
  @Min(1)
  @Max(5)
  rating!: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  @MaxLength(500)
  comment?: string;
}
