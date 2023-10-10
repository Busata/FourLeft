import {Component, EventEmitter, Input, Output} from '@angular/core';
import {GalleryPhotoTo} from "@server-models";

@Component({
  selector: 'app-photo-stream',
  templateUrl: './photo-stream.component.html',
  styleUrls: ['./photo-stream.component.scss']
})
export class PhotoStreamComponent {

  @Input()
  public images: GalleryPhotoTo[] = [];

  @Input()
  public enableControls: boolean = false;

  @Output()
  public deleteRequest = new EventEmitter<GalleryPhotoTo>();

  @Output()
  public save = new EventEmitter<any>();

  editingPhoto: any= null;


  getImageUrl(picture: GalleryPhotoTo) {
    return `https://rendercache.busata.io/${picture.id}/fit_height/1080`
  }

  delete(photo: GalleryPhotoTo) {
    if(confirm("This will delete the picture from your profile and the public gallery. Are you sure?")) {
      this.deleteRequest.emit(photo);
    }
  }

    editTags(image: GalleryPhotoTo) {
      this.editingPhoto = image;
    }

  stopEdit() {
    this.editingPhoto = null;
  }

  update(photo: GalleryPhotoTo, data: any) {
    this.save.emit({
      id: photo.id,
      ...data
    })

    this.editingPhoto = null;
  }
}
