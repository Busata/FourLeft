import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {discordCallbackGuard} from "./discord-callback/discord-callback.guard";
import {
  DiscordPageComponent
} from "./discord-page/discord-page.component";
import {discordAuthenticationGuard} from "./discord-callback/discord-authentication.guard";
import {DiscordAuthenticationComponent} from "./discord-authentication/discord-authentication.component";
import {DiscordHomeComponent} from "./discord-home/discord-home.component";

const routes: Routes = [
  {
    path: '',
    canActivate: [discordAuthenticationGuard],
    component: DiscordPageComponent,
    children: [
      {
        path: '',
        component: DiscordHomeComponent,

      },
      {
        path: 'callback',
        canActivate: [discordCallbackGuard],
        children: []
      },
      {
        path: 'authenticate',
        component: DiscordAuthenticationComponent
      }
    ]
  }
]


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DiscordRoutingModule {}
