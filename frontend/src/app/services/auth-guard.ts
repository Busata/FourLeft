import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';

import { AuthService } from './auth';

/** Gates ACRally routes that require a signed-in user, bouncing anonymous visitors to login. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const loginTree = router.createUrlTree(['/acrally/login']);

  if (auth.resolved()) {
    return auth.isAuthenticated() ? true : loginTree;
  }

  return auth.loadMe().pipe(map((user) => (user ? true : loginTree)));
};
