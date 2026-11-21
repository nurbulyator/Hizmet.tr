import { createParamDecorator, ExecutionContext } from '@nestjs/common';
import { AuthRequest, AuthUser } from './jwt-auth.guard';

export const CurrentUser = createParamDecorator(
  (_data: unknown, context: ExecutionContext): AuthUser =>
    context.switchToHttp().getRequest<AuthRequest>().user,
);
