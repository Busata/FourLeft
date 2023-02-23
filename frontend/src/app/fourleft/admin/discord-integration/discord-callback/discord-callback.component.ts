import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {DiscordIntegrationService} from "../discord-integration.service";

@Component({
  selector: 'app-discord-callback',
  templateUrl: './discord-callback.component.html',
  styleUrls: ['./discord-callback.component.scss']
})
export class DiscordCallbackComponent implements OnInit {

  constructor(private route: ActivatedRoute, private router: Router, private discordIntegrationService: DiscordIntegrationService) { }

  ngOnInit(): void {
    const token = <string> this.route.snapshot.queryParamMap.get('code');
    this.discordIntegrationService.postDiscordToken(token).subscribe(() => {
      window?.top?.close();
    })
  }

}

