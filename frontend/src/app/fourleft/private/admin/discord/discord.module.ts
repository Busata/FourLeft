import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {DiscordRoutingModule} from "./discord-routing.module";
import {
  DiscordPageComponent
} from "./discord-page/discord-page.component";
import {DiscordIntegrationApiService} from "./discord-integration-api.service";
import { DiscordAuthenticationComponent } from './discord-authentication/discord-authentication.component';
import { DiscordHomeComponent } from './discord-home/discord-home.component';
import {DiscordStateService} from "./discord-state.service";
import {MatButtonModule} from "@angular/material/button";
import { ManageDiscordServerComponent } from './manage-server/manage-discord-server/manage-discord-server.component';
import { ManageDiscordServerContainerComponent } from './manage-server/manage-discord-server-container/manage-discord-server-container.component';
import { ConfigureChannelContainerComponent } from './configure-channel/configure-channel-container/configure-channel-container.component';
import {MatCardModule} from "@angular/material/card";



@NgModule({
  declarations: [
    DiscordPageComponent,
    DiscordAuthenticationComponent,
    DiscordHomeComponent,
    ManageDiscordServerComponent,
    ManageDiscordServerContainerComponent,
    ConfigureChannelContainerComponent,
  ],
  providers: [DiscordStateService, DiscordIntegrationApiService],
    imports: [
        DiscordRoutingModule,
        CommonModule,
        MatButtonModule,
        MatCardModule,
    ]
})
export class DiscordModule { }
