import { ApiProperty } from '@nestjs/swagger';
import { IsString, Length, Matches } from 'class-validator';

// Turkish mobile number in E.164 format, e.g. +905321234567
const PHONE_REGEX = /^\+90\d{10}$/;

export class RequestCodeDto {
  @ApiProperty({ example: '+905321234567' })
  @Matches(PHONE_REGEX, { message: 'phone must look like +905321234567' })
  phone!: string;
}

export class VerifyCodeDto extends RequestCodeDto {
  @ApiProperty({ example: '1234' })
  @IsString()
  @Length(4, 4)
  code!: string;
}

export class RefreshDto {
  @ApiProperty()
  @IsString()
  refreshToken!: string;
}
