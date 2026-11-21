import { ApiPropertyOptional, ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsIn, IsInt, IsOptional, IsString, Matches } from 'class-validator';

export class SpecialistQueryDto {
  @ApiPropertyOptional({ example: 'İstanbul' })
  @IsOptional()
  @IsString()
  city?: string;

  @ApiPropertyOptional({ example: 'manicure' })
  @IsOptional()
  @IsString()
  category?: string;

  @ApiPropertyOptional({ description: 'Search by name, title or service' })
  @IsOptional()
  @IsString()
  q?: string;

  @ApiPropertyOptional({ enum: ['rating', 'price'] })
  @IsOptional()
  @IsIn(['rating', 'price'])
  sort?: 'rating' | 'price';
}

export class SlotsQueryDto {
  @ApiProperty()
  @Type(() => Number)
  @IsInt()
  serviceId!: number;

  @ApiProperty({ example: '2026-10-21' })
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  date!: string;
}
