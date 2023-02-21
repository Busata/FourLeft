import { Component } from '@angular/core';
import {AuthenticationService} from "./authentication.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'fourleft-frontend';


  constructor(private authenticationService: AuthenticationService) {
      this.authenticationService.configure();
  }

}
