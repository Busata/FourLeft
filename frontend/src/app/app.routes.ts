import { Routes } from '@angular/router';
import { Shell } from './shell/shell';
import { Home } from './pages/home/home';
import { Privacy } from './pages/privacy/privacy';
import { Terms } from './pages/terms/terms';
import { ProfileInfo } from './pages/profile-info/profile-info';
import { ProfileEditor } from './pages/profile-editor/profile-editor';
import { ChannelConfig } from './pages/channel-config/channel-config';
import { Status } from './pages/status/status';
import { WorkQueue } from './pages/work-queue/work-queue';
import { TimeTrials } from './pages/time-trials/time-trials';
import { TimeTrialsShell } from './pages/time-trials-shell/time-trials-shell';
import { TimeTrialsBoards } from './pages/time-trials-boards/time-trials-boards';
import { TimeTrialsProfile } from './pages/time-trials-profile/time-trials-profile';
import { ClubCompare } from './pages/club-compare/club-compare';
import { AcrallyLogin } from './pages/acrally-login/acrally-login';
import { AcrallyRegister } from './pages/acrally-register/acrally-register';
import { AcrallyAccount } from './pages/acrally-account/acrally-account';
import { AcrallyLink } from './pages/acrally-link/acrally-link';
import { AcrallyDashboard } from './pages/acrally-dashboard/acrally-dashboard';
import { AcrallyClubs } from './pages/acrally-clubs/acrally-clubs';
import { AcrallyStats } from './pages/acrally-stats/acrally-stats';
import { AcrallyUsers } from './pages/acrally-users/acrally-users';
import { authGuard } from './services/auth-guard';
import { adminGuard } from './services/admin-guard';

export const routes: Routes = [
  {
    path: '',
    component: Shell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      { path: 'home', component: Home, title: 'fourleft.io' },
      { path: 'easportswrc/profile', component: ProfileInfo, title: 'EA Sports WRC Profile' },
      { path: 'easportswrc/profile/:requestId', component: ProfileEditor, title: 'EA Sports WRC Profile' },
      { path: 'easportswrc/channel/:requestId', component: ChannelConfig, title: 'Channel Configuration' },
      {
        path: 'easportswrc/status',
        component: Status,
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'queue' },
          { path: 'queue', component: WorkQueue, title: 'Status' },
          { path: 'time-trials', component: TimeTrials, title: 'Time Trials Coverage' },
        ],
      },
      {
        path: 'easportswrc/time-trials',
        component: TimeTrialsShell,
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'boards' },
          { path: 'boards', component: TimeTrialsBoards, title: 'Time Trials' },
          { path: 'profile', component: TimeTrialsProfile, title: 'Time Trials Profile' },
        ],
      },
      { path: 'easportswrc/club-compare', component: ClubCompare, title: 'Club Compare' },
      { path: 'acrally/login', component: AcrallyLogin, title: 'AC Rally — Sign in' },
      { path: 'acrally/register', component: AcrallyRegister, title: 'AC Rally — Create account' },
      {
        path: 'acrally/dashboard',
        component: AcrallyDashboard,
        canActivate: [authGuard],
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'clubs' },
          { path: 'clubs', component: AcrallyClubs, title: 'AC Rally — Clubs' },
          { path: 'stats', component: AcrallyStats, title: 'AC Rally — Personal stats' },
          { path: 'account', component: AcrallyAccount, title: 'AC Rally — Account' },
          { path: 'users', component: AcrallyUsers, canActivate: [adminGuard], title: 'AC Rally — Users' },
        ],
      },
      // Old standalone account URL (Steam link return path, bookmarks) → the account tab.
      { path: 'acrally/account', pathMatch: 'full', redirectTo: 'acrally/dashboard/account' },
      // Public route: the page bounces to login itself, preserving the pairing code.
      { path: 'acrally/link', component: AcrallyLink, title: 'AC Rally — Authorize device' },
      // Keep the old top-level work-queue URL working (bookmarks, links).
      { path: 'easportswrc/work-queue', pathMatch: 'full', redirectTo: 'easportswrc/status/queue' },
      { path: 'privacy', component: Privacy, title: 'Privacy Policy' },
      { path: 'terms', component: Terms, title: 'Terms of Service' },
    ],
  },
  { path: '**', redirectTo: '' },
];
