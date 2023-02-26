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



@NgModule({
  declarations: [
    DiscordPageComponent,
    DiscordAuthenticationComponent,
    DiscordHomeComponent,
  ],
  providers: [DiscordStateService, DiscordIntegrationApiService],
  imports: [
    DiscordRoutingModule,
    CommonModule,
    MatButtonModule,
  ]
})
export class DiscordModule { }
