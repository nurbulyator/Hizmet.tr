import {
  Body,
  Controller,
  Get,
  HttpCode,
  Post,
  UseGuards,
} from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { PrismaService } from '../prisma/prisma.service';
import { RefreshDto, RequestCodeDto, VerifyCodeDto } from './auth.dto';
import { AuthService } from './auth.service';
import { CurrentUser } from './current-user.decorator';
import { AuthUser, JwtAuthGuard } from './jwt-auth.guard';

@ApiTags('auth')
@Controller('auth')
export class AuthController {
  constructor(
    private readonly auth: AuthService,
    private readonly prisma: PrismaService,
  ) {}

  @Post('request-code')
  @HttpCode(200)
  requestCode(@Body() dto: RequestCodeDto) {
    return this.auth.requestCode(dto.phone);
  }

  @Post('verify')
  @HttpCode(200)
  verify(@Body() dto: VerifyCodeDto) {
    return this.auth.verifyCode(dto.phone, dto.code);
  }

  @Post('refresh')
  @HttpCode(200)
  refresh(@Body() dto: RefreshDto) {
    return this.auth.refresh(dto.refreshToken);
  }

  @Get('me')
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  me(@CurrentUser() user: AuthUser) {
    return this.prisma.user.findUniqueOrThrow({ where: { id: user.id } });
  }
}
