import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';

import { AuthService } from './auth';

/**
 * Gates ACRally admin routes: anonymous visitors go to login, signed-in non-admins are bounced
 * back to the dashboard. The backend /acrally-api/admin/** rules are the real gate; this just
 * keeps the UI honest.
 */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const loginTree = router.createUrlTree(['/acrally/login']);
  const dashboardTree = router.createUrlTree(['/acrally/dashboard']);

  const resolve = () => {
    if (!auth.isAuthenticated()) {
      return loginTree;
    }
    return auth.isAdmin() ? true : dashboardTree;
  };

  if (auth.resolved()) {
    return resolve();
  }

  return auth.loadMe().pipe(map(() => resolve()));
};
