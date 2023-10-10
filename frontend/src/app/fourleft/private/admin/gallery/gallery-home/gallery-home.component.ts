import {Component, OnInit} from '@angular/core';
import {GalleryApiService} from "../infrastructure/clients/gallery-api.service";
import {Router} from "@angular/router";
import {GalleryPhotoTo} from "@server-models";

@Component({
  selector: 'app-gallery-home',
  templateUrl: './gallery-home.component.html',
  styleUrls: ['./gallery-home.component.scss']
})
export class GalleryHomeComponent implements OnInit {

  public pictures: GalleryPhotoTo[] = [];

  public authenticated = false;

  constructor(private galleryApiService: GalleryApiService, private router: Router) {
    this.galleryApiService.isAuthenticated$.subscribe((status: any) => {
      this.authenticated = status.authenticated;
    });
  }

  onPictureUpload($event: GalleryPhotoTo) {
    this.pictures.push($event);
  }


  onPictureDelete(image: GalleryPhotoTo) {
    this.galleryApiService.deleteImage(image.id).subscribe(() => {
      this.pictures = this.pictures.filter(photo => photo.id != image.id);
    });
  }

  getImageUrl(pictureId: string) {
    return `https://rendercache.busata.io/${pictureId}/fit_height/1080`
  }

  ngOnInit(): void {
    this.galleryApiService.getUserImages().subscribe(pictures => {
      this.pictures.push(...pictures);
    })
  }

  onPictureSave($event: string) {
    this.galleryApiService.updatePicture($event).subscribe(photo => {
      this.pictures = this.pictures.map(p => {
        if(p.id == photo.id) {
          return photo;
        } else {
          return p;
        }
      });
    });
  }
}
