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
import { ClubViewFormComponent } from './configure-channel/club-view-form/club-view-form.component';
import { ResultsViewFormComponent } from './configure-channel/results-view-form/results-view-form.component';
import {ReactiveFormsModule} from "@angular/forms";
import { SingleClubViewFormComponent } from './configure-channel/single-club-view-form/single-club-view-form.component';
import {MergeViewClubFormComponent} from "./configure-channel/merge-club-view-form/merge-view-club-form.component";
import { PartitionClubViewFormComponent } from './configure-channel/partition-club-view-form/partition-club-view-form.component';
import { PartitionElementFormComponent } from './configure-channel/partition-element-form/partition-element-form.component';
import { FixedPointsFormComponent } from './configure-channel/fixed-points-form/fixed-points-form.component';
import { DiscordChannelConfigurationFormComponent } from './configure-channel/discord-channel-configuration-form/discord-channel-configuration-form.component';
import { PreviewChannelConfigurationComponent } from './configure-channel/preview-channel-configuration/preview-channel-configuration.component';



@NgModule({
  declarations: [
    DiscordPageComponent,
    DiscordAuthenticationComponent,
    DiscordHomeComponent,
    ManageDiscordServerComponent,
    ManageDiscordServerContainerComponent,
    ConfigureChannelContainerComponent,
    ClubViewFormComponent,
    MergeViewClubFormComponent,
    ResultsViewFormComponent,
    SingleClubViewFormComponent,
    PartitionClubViewFormComponent,
    PartitionElementFormComponent,
    FixedPointsFormComponent,
    DiscordChannelConfigurationFormComponent,
    PreviewChannelConfigurationComponent,
  ],
  providers: [DiscordStateService, DiscordIntegrationApiService],
    imports: [
        DiscordRoutingModule,
        CommonModule,
        MatButtonModule,
        MatCardModule,
        ReactiveFormsModule,
    ]
})
export class DiscordModule { }
