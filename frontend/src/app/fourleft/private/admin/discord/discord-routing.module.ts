import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {discordCallbackGuard} from "./discord-callback/discord-callback.guard";
import {
  DiscordPageComponent
} from "./discord-page/discord-page.component";
import {discordAuthenticationGuard} from "./discord-callback/discord-authentication.guard";
import {DiscordAuthenticationComponent} from "./discord-authentication/discord-authentication.component";
import {DiscordHomeComponent} from "./discord-home/discord-home.component";
import {ManageDiscordServerComponent} from "./manage-server/manage-discord-server/manage-discord-server.component";
import {discordManageServerGuard} from "./manage-server/discord-manage-server.guard";
import {
  ManageDiscordServerContainerComponent
} from "./manage-server/manage-discord-server-container/manage-discord-server-container.component";
import {
  ConfigureChannelContainerComponent
} from "./configure-channel/configure-channel-container/configure-channel-container.component";

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
      },
      {
        path: 'configure/:guildId',
        canActivate: [discordManageServerGuard],
        component: ManageDiscordServerContainerComponent
      },
      {
        path: 'configure/:guildId/channels/:channelId',
        canActivate: [discordManageServerGuard],
        component: ConfigureChannelContainerComponent
      }
    ]
  }
]


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DiscordRoutingModule {}
