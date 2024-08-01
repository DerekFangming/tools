export class Post {
    id: number | undefined
    title: string | undefined
    imageNames: string[] = []
    imageUrls: string[] = []
    attachment: string | undefined
    rank: number | undefined
    category: number | undefined
    flagged: boolean | undefined
    url: string | undefined
    created: string | undefined
    saved: boolean | undefined

    expanded: boolean | undefined

	constructor() {}
}

export const PostCatMap = {
    All: 0,
    Hua: 798,
    Asian: 96,
    CloudFast: 427,
    Cloud: 103,
    US: 135,
    AsiDongn: 136,
    HuaOrig: 280,
    AsianOrig: 723,
    USOrig: 525,
    TeYao: 232,
    New: 233
}