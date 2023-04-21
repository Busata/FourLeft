import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {DiscordChannelConfigurationTo} from '@server-models';

@Component({
  selector: 'app-management-container',
  templateUrl: './management-container.component.html',
  styleUrls: ['./management-container.component.scss']
})
export class ManagementContainerComponent implements OnInit {

  public configurations: DiscordChannelConfigurationTo[] = [];
  constructor(private http: HttpClient) {
  }

  triggerUpdate(value: string) {
    this.http.post('/api/management/update_leaderboard', {clubId: value}).subscribe();
  }

  ngOnInit(): void {
    this.http.get<DiscordChannelConfigurationTo[]>('/api/discord/configurations').subscribe(configurations => {
      this.configurations = configurations;
    })
  }

  removeConfiguration(configuration: DiscordChannelConfigurationTo) {
    this.http.delete(`/api/discord/configurations/${configuration.id}`).subscribe();
  }

  triggerImportTicker() {
    this.http.post<any>(`/api/management/import_ticker`, {}).subscribe();

  }
}
