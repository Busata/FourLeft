import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {FourleftPrivateContainerComponent} from "./fourleft-private-container/fourleft-private-container.component";
import {UsersListComponent} from "./admin/users/users-list/users-list.component";
import {FieldMappingsListComponent} from "./admin/field-mappings/field-mappings-list/field-mappings-list.component";
import {ManagementContainerComponent} from './admin/management/management-container/management-container.component';

export const routes: Routes = [
  {
    path: '',
    component: FourleftPrivateContainerComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'discord'
      },
      {
        path: 'discord',
        loadChildren: () => import('./admin/discord/discord.module').then(m => m.DiscordModule)
      },
      {
        path: 'users',
        component: UsersListComponent
      }, {
        path: 'field_mappings',
        component: FieldMappingsListComponent
      },
      {
        path: 'management',
        component: ManagementContainerComponent
      },
      {
        path: 'gallery',
        loadChildren: () => import('./admin/gallery/gallery.module').then(m => m.GalleryModule)

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
