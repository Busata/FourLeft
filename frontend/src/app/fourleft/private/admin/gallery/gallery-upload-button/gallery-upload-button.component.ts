import {Component, EventEmitter, Output} from '@angular/core';
import {GalleryApiService} from "../infrastructure/clients/gallery-api.service";
import {GalleryPhotoTo} from "@server-models";

@Component({
  selector: 'app-gallery-upload-button',
  templateUrl: './gallery-upload-button.component.html',
  styleUrls: ['./gallery-upload-button.component.scss']
})
export class GalleryUploadButtonComponent {


  @Output()
  public uploaded = new EventEmitter<GalleryPhotoTo>();

  constructor(private galleryApiService: GalleryApiService) {
  }

  processFile($event: any) {
    let file = $event.target.files[0];

      this.galleryApiService.uploadImage(file).subscribe(photo => {
        this.uploaded.emit(photo);
      });

  }
}
