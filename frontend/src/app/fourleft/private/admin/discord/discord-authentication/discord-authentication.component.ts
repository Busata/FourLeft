import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";

@Component({
  selector: 'app-discord-login',
  templateUrl: './discord-authentication.component.html',
  styleUrls: ['./discord-authentication.component.scss']
})
export class DiscordAuthenticationComponent implements OnInit{

  constructor(private router: Router) {
  }
  ngOnInit(): void {
    this.authWithDiscord();
  }


  authWithDiscord() {
    let discordAuthentication = <Window> window.open("/api/public/discord/redirect", 'popup', 'popup,width=600,height=700')
    let timer = setInterval(() => {
      if(discordAuthentication.closed) {
        clearInterval(timer);
        this.router.navigate(['/private/discord']);
      }
    }, 1000);
  }

}
