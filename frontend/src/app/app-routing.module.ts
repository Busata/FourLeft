import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';


const routes: Routes = [
  {
    path:'',
    loadChildren: () => import('./fourleft/public/fourleft-public.module').then(m => m.FourleftPublicModule)
  },
  {
    path: 'private',
    loadChildren: () => import('./fourleft/private/fourleft-private.module').then(m => m.FourleftPrivateModule)
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
