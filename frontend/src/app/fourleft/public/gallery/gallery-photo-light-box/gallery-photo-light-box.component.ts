import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-gallery-photo-light-box',
  templateUrl: './gallery-photo-light-box.component.html',
  styleUrls: ['./gallery-photo-light-box.component.scss']
})
export class GalleryPhotoLightBoxComponent {

  @Input()
  photoId!: String;

  @Output()
  close = new EventEmitter();

  getUrl() {
    return `https://rendercache.busata.io/${this.photoId}`;
  }
}
