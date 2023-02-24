import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "../services/authentication.service";

@Component({
  selector: 'app-app-container',
  templateUrl: './fourleft-private-container.component.html',
  styleUrls: ['./fourleft-private-container.component.scss']
})
export class FourleftPrivateContainerComponent implements OnInit{

  constructor(private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
    this.authenticationService.configure();
  }

}
