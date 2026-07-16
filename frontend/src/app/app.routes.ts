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
import { AcrallyAccount } from './pages/acrally-account/acrally-account';
import { AcrallyLink } from './pages/acrally-link/acrally-link';
import { AcrallyDashboard } from './pages/acrally-dashboard/acrally-dashboard';
import { AcrallyClubs } from './pages/acrally-clubs/acrally-clubs';
import { AcrallyClubDetail } from './pages/acrally-club-detail/acrally-club-detail';
import { AcrallyChampionship } from './pages/acrally-championship/acrally-championship';
import { AcrallyStats } from './pages/acrally-stats/acrally-stats';
import { AcrallyAdmin } from './pages/acrally-admin/acrally-admin';
import { AcrallyUsers } from './pages/acrally-users/acrally-users';
import { AcrallyLocations } from './pages/acrally-locations/acrally-locations';
import { AcrallyStages } from './pages/acrally-stages/acrally-stages';
import { AcrallyVariants } from './pages/acrally-variants/acrally-variants';
import { AcrallyCars } from './pages/acrally-cars/acrally-cars';
import { AcrallyCarAliases } from './pages/acrally-car-aliases/acrally-car-aliases';
import { AcrallyTrackAliases } from './pages/acrally-track-aliases/acrally-track-aliases';
import { AcrallyIssues } from './pages/acrally-issues/acrally-issues';
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
      {
        path: 'acrally/dashboard',
        component: AcrallyDashboard,
        canActivate: [authGuard],
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'clubs' },
          { path: 'clubs', component: AcrallyClubs, title: 'AC Rally — Clubs' },
          { path: 'clubs/:clubId', component: AcrallyClubDetail, title: 'AC Rally — Club' },
          {
            path: 'clubs/:clubId/championships/:championshipId',
            component: AcrallyChampionship,
            title: 'AC Rally — Championship',
          },
          { path: 'stats', component: AcrallyStats, title: 'AC Rally — Personal stats' },
          { path: 'account', component: AcrallyAccount, title: 'AC Rally — Account' },
          {
            path: 'admin',
            component: AcrallyAdmin,
            canActivate: [adminGuard],
            children: [
              { path: '', pathMatch: 'full', redirectTo: 'users' },
              { path: 'users', component: AcrallyUsers, title: 'AC Rally — Users' },
              { path: 'locations', component: AcrallyLocations, title: 'AC Rally — Locations' },
              { path: 'stages', component: AcrallyStages, title: 'AC Rally — Stages' },
              { path: 'variants', component: AcrallyVariants, title: 'AC Rally — Variants' },
              { path: 'cars', component: AcrallyCars, title: 'AC Rally — Cars' },
              { path: 'car-aliases', component: AcrallyCarAliases, title: 'AC Rally — Car aliases' },
              { path: 'track-aliases', component: AcrallyTrackAliases, title: 'AC Rally — Track aliases' },
              { path: 'issues', component: AcrallyIssues, title: 'AC Rally — Issue reports' },
            ],
          },
          // Old admin URL (bookmarks) → the admin section's Users tab.
          { path: 'users', pathMatch: 'full', redirectTo: 'admin/users' },
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
