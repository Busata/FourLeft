import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {UsersListComponent} from "./fourleft/admin/users/users-list/users-list.component";
import {FieldMappingsListComponent} from "./fourleft/admin/field-mappings/field-mappings-list/field-mappings-list.component";
import {ClubMergeResultsComponent} from "./fourleft/admin/club-merger/club-merge-results/club-merge-results.component";
import {ClubTiersContainerComponent} from './fourleft/admin/club-tiers/club-tiers-container/club-tiers-container.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: '/'
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
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true, initialNavigation: "disabled"})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
