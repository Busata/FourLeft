import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {FourleftPrivateContainerComponent} from "./fourleft-private-container/fourleft-private-container.component";
import {ManagementContainerComponent} from './admin/management/management-container/management-container.component';

export const routes: Routes = [
  {
    path: '',
    component: FourleftPrivateContainerComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'management'
      },
      {
        path: 'management',
        component: ManagementContainerComponent
      }
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FourleftPrivateRoutingModule {
}
