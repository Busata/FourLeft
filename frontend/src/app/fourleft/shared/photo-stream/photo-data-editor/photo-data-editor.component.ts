import {Component, EventEmitter, Input, Output} from '@angular/core';
import {GalleryApiService} from "../../../private/admin/gallery/infrastructure/clients/gallery-api.service";
import {GalleryPhotoTo, GalleryTagNodeTo} from "@server-models";


@Component({
  selector: 'app-photo-tag-editor',
  templateUrl: './photo-data-editor.component.html',
  styleUrls: ['./photo-data-editor.component.scss']
})
export class PhotoDataEditor {

  public _photo!: GalleryPhotoTo;

  @Input()
  public set photo(value: GalleryPhotoTo) {
    this._photo = value;

    this.title = value.title;
    this.published = value.published;
    this.preview = value.preview;
  }

  @Output()
  close = new EventEmitter();

  @Output()
  save = new EventEmitter();

  public title: string = "";
  public published = false;
  public preview = false;

  public nodes!: GalleryTagNodeTo[];

  private get selectedNodes() {
    return Array.from(this.selections.values());
  }

  private selections: Map<GalleryTagNodeTo, string> = new Map();

  constructor(private galleryApiService: GalleryApiService) {
    this.galleryApiService.getTagNodes().subscribe(nodes => {
      this.nodes = nodes;

      if (this._photo) {
        nodes.forEach(masterNode => {
          masterNode.nodes.forEach(childNode => {

            this._photo.tags.forEach(tag => {
              if (childNode.id == tag) {
                this.selections.set(masterNode, tag);
              }
            })

          });
        });
      }
    })
  }

  getImageUrl() {
    return `https://rendercache.busata.io/${this._photo.id}/fit_height/1080`
  }

  get visibleNodes() {
    return this.nodes.filter(node => (node.showOn == undefined) || this.selectedNodes.indexOf(node.showOn) !== -1);
  }


  selectOption(node: GalleryTagNodeTo, newSelection: any) {
    this.selections.set(node, newSelection);

    this.validateSelection();

  }

  validateSelection() {

    let hasDeleted = false;

    do {
      let values = Array.from(this.selections.values());
      let keys = Array.from(this.selections.keys());
      hasDeleted = false;

      for (let key of keys) {

        if (key.showOn == undefined) {
          continue;
        }

        if (values.indexOf(key.showOn) == -1) {
          this.selections.delete(key);
          hasDeleted = true;
        }
      }

    }
    while (hasDeleted);

  }

  saveData() {
    this.save.emit({
      title: this.title,
      published: this.published,
      preview: this.preview,
      tags: Array.from(this.selections.values())
    })

  }

  setTitle(value: any) {
    this.title = value;
  }
  setPublished(value: any) {
    this.published = value;
  }
  setPreview(value: any) {
    this.preview = value;
  }

  getSelected(node: GalleryTagNodeTo) {
    return this.selections.get(node);
  }
}
