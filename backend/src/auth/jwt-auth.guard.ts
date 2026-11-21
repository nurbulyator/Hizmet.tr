import {
  CanActivate,
  ExecutionContext,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { Request } from 'express';

export class AuthUser {
  id!: number;
}

export interface AuthRequest extends Request {
  user: AuthUser;
}

@Injectable()
export class JwtAuthGuard implements CanActivate {
  constructor(private readonly jwt: JwtService) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest<AuthRequest>();
    const [type, token] = (request.headers.authorization ?? '').split(' ');
    if (type !== 'Bearer' || !token) {
      throw new UnauthorizedException();
    }
    try {
      const payload = await this.jwt.verifyAsync<{ sub: number }>(token);
      request.user = { id: payload.sub };
      return true;
    } catch {
      throw new UnauthorizedException();
    }
  }
}
