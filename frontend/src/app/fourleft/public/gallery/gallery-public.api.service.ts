import {Inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {GalleryPublicPhotoTo} from "@server-models";
import {Observable} from "rxjs";


@Injectable({
  providedIn: 'root'
})
export class GalleryPublicApiService {



  constructor(private http: HttpClient) {
  }


  getImages(query: string, page: number): Observable<GalleryPublicPhotoTo[]> {
    return this.http.get<GalleryPublicPhotoTo[]>(`/api/external/gallery/photos?query=${query}&page=${page}`)
  }
}
