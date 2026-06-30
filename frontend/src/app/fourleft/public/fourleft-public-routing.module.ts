import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {FourleftPublicContainerComponent} from "./fourleft-public-container/fourleft-public-container.component";
import {HomeComponent} from './home/home.component';
import {EASportsWRCPageComponent} from "./easportswrc/easports-wrcpage/easports-wrcpage.component";
import {
  EASportsWRCProfileRedirectComponent
} from "./easportswrc/profile/easports-wrc-profile-redirect/easportswrc-profile-redirect.component";
import {
  EASportsWRCProfileContainerComponent
} from "./easportswrc/profile/easports-wrc-profile-container/easports-wrcprofile-container.component";
import {PrivacyPolicyComponent} from "./legal/privacy-policy.component";
import {TermsOfServiceComponent} from "./legal/terms-of-service.component";

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
        path: 'easportswrc',
        component: EASportsWRCPageComponent,
        children: [
          {
            path: 'profile',
            children: [
              {
                path: '',
                pathMatch:'full',
                component: EASportsWRCProfileRedirectComponent
              },
              {
                path: ':requestId',
                pathMatch: 'full',
                component: EASportsWRCProfileContainerComponent
              }
            ]
          }
        ]
      },
      {
        path: 'privacy',
        pathMatch: 'full',
        component: PrivacyPolicyComponent
      },
      {
        path: 'terms',
        pathMatch: 'full',
        component: TermsOfServiceComponent
      }
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FourleftPublicRoutingModule {
}
