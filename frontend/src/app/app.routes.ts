import { Routes } from '@angular/router';
import { Shell } from './shell/shell';
import { Home } from './pages/home/home';
import { Privacy } from './pages/privacy/privacy';
import { Terms } from './pages/terms/terms';
import { ProfileInfo } from './pages/profile-info/profile-info';
import { ProfileEditor } from './pages/profile-editor/profile-editor';
import { ImportQueue } from './pages/import-queue/import-queue';

export const routes: Routes = [
  {
    path: '',
    component: Shell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      { path: 'home', component: Home, title: 'fourleft.io' },
      { path: 'easportswrc/profile', component: ProfileInfo, title: 'EA Sports WRC Profile' },
      { path: 'easportswrc/profile/:requestId', component: ProfileEditor, title: 'EA Sports WRC Profile' },
      { path: 'easportswrc/import-queue', component: ImportQueue, title: 'Import Queue' },
      { path: 'privacy', component: Privacy, title: 'Privacy Policy' },
      { path: 'terms', component: Terms, title: 'Terms of Service' },
    ],
  },
  { path: '**', redirectTo: '' },
];
