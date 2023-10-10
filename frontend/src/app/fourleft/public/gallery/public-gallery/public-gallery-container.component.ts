import {Component, HostListener, OnInit} from '@angular/core';
import {GalleryPublicPhotoTo} from "@server-models";
import {GalleryPublicApiService} from "../gallery-public.api.service";
import {debounceTime, distinctUntilChanged, Subject} from "rxjs";

@Component({
  selector: 'app-public-gallery',
  templateUrl: './public-gallery-container.component.html',
  styleUrls: ['./public-gallery-container.component.scss']
})
export class PublicGalleryContainerComponent implements OnInit{

  public queryChanged = new Subject<string>();

  public photos: GalleryPublicPhotoTo[] = [];

  public page = 0;

  public query = "";

  public selectedPhoto: GalleryPublicPhotoTo | undefined | null;

  constructor(private galleryPublicApi: GalleryPublicApiService) {
  }

  @HostListener('document:keydown.escape', ['$event']) onKeydownHandler(event: KeyboardEvent) {
    this.deselectPhoto();
  }

  ngOnInit(): void {
    this.galleryPublicApi.getImages(this.query, this.page).subscribe(images => {
      this.photos = images;
    });

    this.queryChanged.pipe(debounceTime(500), distinctUntilChanged()).subscribe(() => {
      this.page = 0;
      this.galleryPublicApi.getImages(this.query, this.page).subscribe(images => {
        this.photos = images;
      })
    })
  }

  getUrl(image: GalleryPublicPhotoTo) {
    return `https://rendercache.busata.io/${image.id}/fit_height/800`
  }


  onScroll() {
    this.page += 1;
    this.galleryPublicApi.getImages(this.query, this.page).subscribe(images => {
      this.photos.push(...images);
    });
  }

  updateQuery(value: any) {
    this.query = value;
    this.queryChanged.next(this.query);

  }

  selectPhoto(photo: GalleryPublicPhotoTo) {
    this.selectedPhoto = photo;
  }

  deselectPhoto() {
    this.selectedPhoto = null;
  }
}
