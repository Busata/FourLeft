import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {FourleftPublicContainerComponent} from "./fourleft-public-container/fourleft-public-container.component";
import {HomeComponent} from './home/home.component';
import {ResultsContainerComponent} from './results/results-container/results-container.component';
import {UserCommunityProgressComponent} from './user-community-progress/user-community-progress.component';
import {ResultDetailsComponent} from './results/result-details/result-details.component';

export const routes: Routes = [
  {
    path: '',
    component: FourleftPublicContainerComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'home',
      },
      {
        path: 'home',
        pathMatch: 'full',
        component: HomeComponent
      },
      {
        path: 'community',
        pathMatch: 'full',
        component: UserCommunityProgressComponent
      },
      {
        path: 'results',
        pathMatch: 'full',
        component: ResultsContainerComponent,
      },
          {
            path: 'results/:id',
            pathMatch:"full",
            component: ResultDetailsComponent
          }
    ]
  },
  {
    path: 'user_community_progress',
    redirectTo: '/',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FourleftPublicRoutingModule {
}
