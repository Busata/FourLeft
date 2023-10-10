import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {GalleryPhotoTo, GalleryTagNodeTo} from "@server-models";

@Injectable({
  providedIn: 'root'
})
export class GalleryApiService {

  public isAuthenticated$ = this.httpClient.get('/api/internal/security/user')
  constructor(private httpClient: HttpClient) {
  }


  uploadImage(result: File) {

    let formData = new FormData();

    formData.append("file", result);



    return this.httpClient.post<GalleryPhotoTo>('/api/internal/gallery/_upload', formData);
  }

  getUserImages() {
    return this.httpClient.get<GalleryPhotoTo[]>('/api/internal/gallery/photos');
  }

  getTagNodes() {
    return this.httpClient.get<GalleryTagNodeTo[]>('/api/internal/gallery/tag_graph');
  }

  deleteImage(imageId: string) {
    return this.httpClient.delete(`/api/internal/gallery/photos/${imageId}`);
  }

  updatePicture($event: any) {
    return this.httpClient.put<GalleryPhotoTo>(`/api/internal/gallery/photos/${$event.id}`, $event)
  }
}
