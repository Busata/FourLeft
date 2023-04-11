import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {FourleftPrivateContainerComponent} from "./fourleft-private-container/fourleft-private-container.component";
import {UsersListComponent} from "./admin/users/users-list/users-list.component";
import {FieldMappingsListComponent} from "./admin/field-mappings/field-mappings-list/field-mappings-list.component";

export const routes: Routes = [
  {
    path: '',
    component: FourleftPrivateContainerComponent,
    children: [
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
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FourleftPrivateRoutingModule {
}
