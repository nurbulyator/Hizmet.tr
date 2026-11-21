import { Injectable, Logger, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { createHash, randomBytes } from 'crypto';
import { PrismaService } from '../prisma/prisma.service';

// SMS is not sent in this project: every phone gets the same code.
// In production an SMS provider would be called in requestCode().
export const FAKE_OTP_CODE = '1234';
const REFRESH_TOKEN_DAYS = 30;

function hashToken(token: string): string {
  return createHash('sha256').update(token).digest('hex');
}

@Injectable()
export class AuthService {
  private readonly logger = new Logger(AuthService.name);

  constructor(
    private readonly prisma: PrismaService,
    private readonly jwt: JwtService,
  ) {}

  requestCode(phone: string) {
    this.logger.log(`OTP for ${phone}: ${FAKE_OTP_CODE}`);
    return { sent: true, resendAfterSeconds: 60 };
  }

  async verifyCode(phone: string, code: string) {
    if (code !== FAKE_OTP_CODE) {
      throw new UnauthorizedException('Wrong code');
    }
    const user = await this.prisma.user.upsert({
      where: { phone },
      update: {},
      create: { phone },
    });
    const tokens = await this.issueTokens(user.id);
    return { ...tokens, user };
  }

  /** Refresh token rotation: the old token is revoked, a new pair is issued. */
  async refresh(refreshToken: string) {
    const stored = await this.prisma.refreshToken.findUnique({
      where: { tokenHash: hashToken(refreshToken) },
    });
    if (!stored || stored.revokedAt || stored.expiresAt < new Date()) {
      throw new UnauthorizedException('Invalid refresh token');
    }
    await this.prisma.refreshToken.update({
      where: { id: stored.id },
      data: { revokedAt: new Date() },
    });
    return this.issueTokens(stored.userId);
  }

  private async issueTokens(userId: number) {
    const accessToken = await this.jwt.signAsync({ sub: userId });
    const refreshToken = randomBytes(32).toString('hex');
    await this.prisma.refreshToken.create({
      data: {
        userId,
        tokenHash: hashToken(refreshToken),
        expiresAt: new Date(Date.now() + REFRESH_TOKEN_DAYS * 86_400_000),
      },
    });
    return { accessToken, refreshToken };
  }
}
