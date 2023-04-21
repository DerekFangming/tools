export class Post {
    id: number;
    title: string;
    imageNames: string[];
    imageUrls: string[];
    attachment: string;
    rank: number;
    category: number;
    flagged: boolean;
    url: string;
    created: string;
    saved: boolean;

    expanded: boolean;

	constructor() {}
}

export enum PostCatMap {
    All = 0,
    Hua = 798,
    Asian = 96,
    CloudFast = 427,
    Cloud = 103,
    US = 135,
    AsiDongn = 136,
    HuaOrig = 280,
    AsianOrig = 723,
    USOrig = 525,
    TeYao = 232,
    New = 233
}
