package com.yasinkacmaz.jetflix.ui.moviedetail

import com.yasinkacmaz.jetflix.data.MovieDetailResponse
import com.yasinkacmaz.jetflix.util.Mapper
import com.yasinkacmaz.jetflix.util.toBackdropUrl
import com.yasinkacmaz.jetflix.util.toPosterUrl
import javax.inject.Inject

class MovieDetailUiModelMapper @Inject constructor() : Mapper<MovieDetailResponse, MovieDetailUiModel> {
    override fun map(input: MovieDetailResponse): MovieDetailUiModel {
        val productionCompanies = input.productionCompanies.map {
            ProductionCompany(it.name, it.logoPath.orEmpty().toPosterUrl())
        }
        return MovieDetailUiModel(
            id = input.id,
            title = input.title,
            originalTitle = input.originalTitle,
            overview = input.overview,
            tagline = input.tagline.dropLastWhile { it == '.' },
            backdropUrl = input.backdropPath.orEmpty().toBackdropUrl(),
            posterUrl = input.posterPath.toPosterUrl(),
            genres = input.genres,
            releaseDate = input.releaseDate,
            voteAverage = input.voteAverage,
            voteCount = input.voteCount,
            duration = input.runtime ?: -1,
            productionCompanies = productionCompanies,
            homepage = input.homepage
        )
    }
}
