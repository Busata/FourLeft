import {Component} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-management-container',
  templateUrl: './management-container.component.html',
  styleUrls: ['./management-container.component.scss']
})
export class ManagementContainerComponent {

  constructor(private http: HttpClient) {
  }

  triggerImportTicker() {
    this.http.post<any>(`/api/internal/management/import_ticker`, {}).subscribe();

  }
}
