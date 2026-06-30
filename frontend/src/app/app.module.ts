import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import {AppRoutingModule} from "./app-routing.module";
import {HttpClientModule} from "@angular/common/http";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import { NgIconsModule } from '@ng-icons/core';
import { tablerSteeringWheel,
  tablerCheck,
    tablerX,
    tablerEyeClosed,
  tablerBookmark,
  tablerTrashFilled,
  tablerBrandXbox,
  tablerBrandSteam,
  tablerPlaystationTriangle,
  tablerPlaystationSquare,
  tablerPlaystationCircle,
  tablerQuestionMark,
  tablerPlaystationX,
  tablerDeviceGamepad,
  tablerKeyboard
} from '@ng-icons/tabler-icons';

@NgModule({
  imports: [
    HttpClientModule,
    AppRoutingModule,
    BrowserModule,
    NgIconsModule.withIcons({ tablerSteeringWheel,
      tablerBrandXbox,
      tablerX,
      tablerBrandSteam,
      tablerPlaystationTriangle,
      tablerEyeClosed,
      tablerPlaystationSquare,
      tablerCheck,
      tablerTrashFilled,
      tablerPlaystationCircle,
      tablerQuestionMark,
      tablerBookmark,
      tablerPlaystationX,
      tablerDeviceGamepad,
      tablerKeyboard
    }),
    BrowserAnimationsModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
  declarations: [
    AppComponent
  ]
})
export class AppModule { }
