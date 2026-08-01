import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';

import type { MediaArchiveFeedTo, MediaArchiveImageTo, MediaArchivePostTo } from '../../models/media-archive';

const IMAGE_STORE_URL = 'https://imagestore.bitsforge.io';

@Component({
  selector: 'app-media-gallery',
  imports: [DatePipe],
  templateUrl: './media-gallery.html',
  styleUrl: './media-gallery.scss',
})
export class MediaGallery implements OnInit {
  private readonly http = inject(HttpClient);

  readonly posts = signal<MediaArchivePostTo[]>([]);
  readonly nextCursor = signal<string | null>(null);
  readonly hasMore = signal(false);
  readonly loading = signal(false);
  readonly loaded = signal(false);

  ngOnInit(): void {
    this.loadPage(null);
  }

  loadMore(): void {
    if (!this.loading()) {
      this.loadPage(this.nextCursor());
    }
  }

  private loadPage(before: string | null): void {
    this.loading.set(true);
    const params: Record<string, string | number> = before ? { before, size: 20 } : { size: 20 };
    this.http.get<MediaArchiveFeedTo>('/api_v2/media-archive/feed', { params }).subscribe({
      next: (feed) => {
        this.posts.update((list) => [...list, ...feed.posts]);
        this.nextCursor.set(feed.nextCursor);
        this.hasMore.set(feed.hasMore);
        this.loading.set(false);
        this.loaded.set(true);
      },
      error: () => {
        this.loading.set(false);
        this.loaded.set(true);
      },
    });
  }

  thumbUrl(image: MediaArchiveImageTo): string {
    return `${IMAGE_STORE_URL}/${image.imageStoreUuid}/fit_width/720`;
  }

  fullUrl(image: MediaArchiveImageTo): string {
    return `${IMAGE_STORE_URL}/${image.imageStoreUuid}`;
  }

  /** Reserves the image's box before it loads, so the feed doesn't jump around. */
  aspectRatio(image: MediaArchiveImageTo): string | null {
    return image.width && image.height ? `${image.width} / ${image.height}` : null;
  }

  authorName(post: MediaArchivePostTo): string {
    return post.authorDisplayName || post.authorUsername || 'unknown';
  }

  initial(post: MediaArchivePostTo): string {
    return this.authorName(post).charAt(0).toUpperCase();
  }

  /** Discord avatar urls go stale when users change avatars; the initial behind the img takes over. */
  hideBrokenAvatar(event: Event): void {
    (event.target as HTMLElement).style.display = 'none';
  }
}
