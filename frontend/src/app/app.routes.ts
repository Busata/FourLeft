import { Routes } from '@angular/router';
import { Shell } from './shell/shell';
import { Home } from './pages/home/home';
import { Privacy } from './pages/privacy/privacy';
import { Terms } from './pages/terms/terms';
import { ProfileInfo } from './pages/profile-info/profile-info';
import { ProfileEditor } from './pages/profile-editor/profile-editor';
import { Status } from './pages/status/status';
import { WorkQueue } from './pages/work-queue/work-queue';
import { TimeTrials } from './pages/time-trials/time-trials';

export const routes: Routes = [
  {
    path: '',
    component: Shell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      { path: 'home', component: Home, title: 'fourleft.io' },
      { path: 'easportswrc/profile', component: ProfileInfo, title: 'EA Sports WRC Profile' },
      { path: 'easportswrc/profile/:requestId', component: ProfileEditor, title: 'EA Sports WRC Profile' },
      {
        path: 'easportswrc/status',
        component: Status,
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'queue' },
          { path: 'queue', component: WorkQueue, title: 'Status' },
          { path: 'time-trials', component: TimeTrials, title: 'Time Trials' },
        ],
      },
      // Keep old top-level URLs working (bookmarks, links).
      { path: 'easportswrc/work-queue', pathMatch: 'full', redirectTo: 'easportswrc/status/queue' },
      { path: 'easportswrc/time-trials', pathMatch: 'full', redirectTo: 'easportswrc/status/time-trials' },
      { path: 'privacy', component: Privacy, title: 'Privacy Policy' },
      { path: 'terms', component: Terms, title: 'Terms of Service' },
    ],
  },
  { path: '**', redirectTo: '' },
];
