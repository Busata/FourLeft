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
    this.http.post('/api/internal/management/update_leaderboard', {clubId: value}).subscribe();
  }
  triggerCommunity() {
    this.http.post('/api/internal/management/update_community', {}).subscribe();
  }

  ngOnInit(): void {
    this.http.get<DiscordChannelConfigurationTo[]>('/api/internal/discord/configurations').subscribe(configurations => {
      this.configurations = configurations;
    })
  }

  removeConfiguration(configuration: DiscordChannelConfigurationTo) {
    this.http.delete(`/api/internal/discord/configurations/${configuration.id}`).subscribe();
  }

  triggerImportTicker() {
    this.http.post<any>(`/api/internal/management/import_ticker`, {}).subscribe();

  }
}
