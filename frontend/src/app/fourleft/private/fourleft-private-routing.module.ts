import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {DiscordCallbackComponent} from "./admin/discord-integration/discord-callback/discord-callback.component";
import {FourleftPrivateContainerComponent} from "./fourleft-private-container/fourleft-private-container.component";
import {
  DiscordIntegrationContainerComponent
} from "./admin/discord-integration/discord-integration-container/discord-integration-container.component";
import {UsersListComponent} from "./admin/users/users-list/users-list.component";
import {FieldMappingsListComponent} from "./admin/field-mappings/field-mappings-list/field-mappings-list.component";
import {ClubMergeResultsComponent} from "./admin/club-merger/club-merge-results/club-merge-results.component";
import {ClubTiersContainerComponent} from "./admin/club-tiers/club-tiers-container/club-tiers-container.component";

export const routes: Routes = [
  {
    path: 'discord_callback',
    component: DiscordCallbackComponent
  },
  {
    path: '',
    component: FourleftPrivateContainerComponent,
    children: [
      {
        path:'discord',
        component: DiscordIntegrationContainerComponent
      },
      {
        path: 'users',
        component: UsersListComponent
      }, {
        path: 'field_mappings',
        component: FieldMappingsListComponent
      },
      {
        path: 'club_merge',
        component: ClubMergeResultsComponent
      },
      {
        path: 'tiers/:id',
        component: ClubTiersContainerComponent
      }
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FourleftPrivateRoutingModule {}
