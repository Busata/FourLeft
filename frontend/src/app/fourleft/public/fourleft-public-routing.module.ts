import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {FourleftPublicContainerComponent} from "./fourleft-public-container/fourleft-public-container.component";
import {UserCommunityProgressComponent} from "./user-community-progress/user-community-progress.component";

export const routes: Routes = [
  {
    path: '',
    component: FourleftPublicContainerComponent,
  },
      {
        path: 'user_community_progress',
        component: UserCommunityProgressComponent
      }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FourleftPublicRoutingModule {}
