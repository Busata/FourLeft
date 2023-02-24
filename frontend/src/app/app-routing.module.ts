import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';


const routes: Routes = [
  {
    path:'',
    pathMatch:'full',
    redirectTo:'public',
  },
  {
    path: 'private',
    loadChildren: () => import('./fourleft/private/fourleft-private.module').then(m => m.FourleftPrivateModule)
  },
  {
    path: 'public',
    loadChildren: () => import('./fourleft/public/fourleft-public.module').then(m => m.FourleftPublicModule)
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
