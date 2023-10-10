import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {FourleftPublicContainerComponent} from "./fourleft-public-container/fourleft-public-container.component";
import {HomeComponent} from './home/home.component';
import {ResultsContainerComponent} from './results/results-container/results-container.component';
import {UserCommunityProgressComponent} from './user-community-progress/user-community-progress.component';
import {ResultDetailsComponent} from './results/result-details/result-details.component';
import {AliasContainerComponent} from "./alias/alias-container/alias-container.component";
import {AliasPageComponent} from "./alias/alias-page/alias-page.component";
import {AliasRedirectComponent} from "./alias/alias-redirect/alias-redirect.component";
import {PublicGalleryContainerComponent} from "./gallery/public-gallery/public-gallery-container.component";

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
        path: 'gallery',
        pathMatch: 'full',
        component: PublicGalleryContainerComponent
      },
      {
        path: 'community',
        pathMatch: 'full',
        component: UserCommunityProgressComponent
      },
      {
        path: 'alias',
        component: AliasPageComponent,
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: AliasRedirectComponent
          },
          {
            path: ':requestId',
            pathMatch: 'full',
            component: AliasContainerComponent
          }]
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
