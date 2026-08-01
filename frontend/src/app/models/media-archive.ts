// Hand-rolled mirrors of the media-archive To records in the api module
// (server-models.d.ts is generated locally but gitignored). Discord snowflake
// ids come over as strings — they exceed Number.MAX_SAFE_INTEGER.

export interface MediaArchiveImageTo {
  imageStoreUuid: string;
  width: number | null;
  height: number | null;
}

export interface MediaArchivePostTo {
  messageId: string;
  authorUsername: string;
  authorDisplayName: string;
  authorAvatarUrl: string;
  caption: string;
  timestamp: string;
  images: MediaArchiveImageTo[];
}

export interface MediaArchiveFeedTo {
  posts: MediaArchivePostTo[];
  nextCursor: string | null;
  hasMore: boolean;
}
